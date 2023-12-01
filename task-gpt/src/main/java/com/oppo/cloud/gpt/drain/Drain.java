/*
 * Copyright 2023 OPPO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oppo.cloud.gpt.drain;


import lombok.Data;

import java.util.*;

@Data
public class Drain {
    /**
     * max depth levels of log clusters, Minimum is 2.
     * For example ,for depth == 4, Root is considered depth level 1.
     * Token count is considered depth level 2.
     * First log token is considered depth level 3.
     * Log clusters below first token node are considered depth level 4.
     */
    private final int logClusterDepth;
    /**
     * max depth of a prefix tree node, starting from zero
     */
    private final int maxNodeDepth;
    /**
     * similarity threshold - if percentage of similar tokens for a log message is below this number,
     * a new log cluster will be created.
     */
    private final double similarityThreshold;
    /**
     * root node for drain tree
     */
    private final Node root;
    /**
     * parameter can be replaced by paramHolder such "<*>"(wildcard)
     */
    private String paramHolder;
    /**
     * Delimiters to apply when splitting log message into words (in addition to whitespace).
     */
    private String[] extraDelimiters;
    /**
     * maximum number of tracked cluster(unlimited by default).
     * When this number is reached, model starts replacing old cluster
     * with a new ones according to the LRU policy.
     */
    private int maxClusters;
    /**
     * maximum number of children of an internal node in the same level,
     * which means the size of `Node.keyToChildNode`
     */
    private int maxChildren;
    /**
     * whether to treat tokens that contains at least one digit as template parameters.
     */
    private boolean parametrizeNumericTokens;
    /**
     * LogCluster id generator
     */
    private IdGenerator idGenerator;
    /**
     * Cache log cluster in order to search.
     */
    private LogClusterCache idToCluster;
    /**
     * Max tokens for a log cluster.
     */
    private int maxTokens;

    public Drain(int logClusterDepth, double similarityThreshold, int maxChildren, int maxClusters, int maxTokens,
                 String[] extraDelimiters, String paramHolder, boolean parametrizeNumericTokens, IdGenerator idGenerator) {
        assert logClusterDepth > 3 : "logClusterDepth must be at least 3";
        this.logClusterDepth = logClusterDepth;
        this.maxNodeDepth = logClusterDepth - 2;
        this.similarityThreshold = similarityThreshold;
        this.maxChildren = maxChildren;
        this.maxTokens = maxTokens;
        this.root = new Node(); // root of searching tree.
        this.extraDelimiters = extraDelimiters;
        this.maxClusters = maxClusters;
        this.paramHolder = paramHolder;
        this.parametrizeNumericTokens = parametrizeNumericTokens;
        this.idToCluster = new LogClusterCache(maxClusters);
        this.idGenerator = idGenerator;
    }

    public Drain() {
        this.logClusterDepth = 4;
        this.maxNodeDepth = this.logClusterDepth - 2;
        this.similarityThreshold = 0.4;
        this.maxChildren = 100;
        this.maxTokens = -1; // -1 means not limited
        this.root = new Node();
        this.extraDelimiters = new String[]{"_"};
        this.maxClusters = -1; // -1 means not limited
        this.paramHolder = "<:*:>";
        this.parametrizeNumericTokens = true;
        this.idToCluster = new LogClusterCache(this.maxClusters);
        this.idGenerator = new SimpleIdGenerator();
    }

    /**
     * @param message            Match message
     * @param fullSearchStrategy never, fallback, always
     *                           1.`never` is the fastest, will always perform a tree search [O(log(n))], but might
     *                           produce false negatives (wrong mismatches) on some edge cases.
     *                           2.`fallback` will perform a linear search[O(log(n))] among all clusters with the same
     *                           token count, but only in case tree search found no match. It should not have false
     *                           negatives, however tree-search may find a non-optimal match with more wildcard
     *                           parameters than necessary.
     *                           3.`always` is the slowest. It will select the best match among all known clusters, by
     *                           always evaluating all clusters with the same token count, and selecting the cluster
     *                           with perfect all token match and least count of wildcard matches.
     * @return Matched cluster or null if no match found
     */
    public LogCluster match(String message, String fullSearchStrategy) {
        String[] tokens = genTokens(message);
        if (this.maxTokens > 0 && tokens.length >= this.maxTokens) {
            tokens = Arrays.copyOf(tokens, this.maxTokens);
        }
        double similarityThreshold = 1.0;
        if (fullSearchStrategy.equals("always")) {
            return fullSearch(tokens, similarityThreshold);
        }
        LogCluster logCluster = this.treeSearch(tokens, similarityThreshold, true);
        if (logCluster != null) {
            return logCluster;
        }
        // fallback is not implemented.
        if (fullSearchStrategy.equals("never")) {
            return null;
        }
        return fullSearch(tokens, similarityThreshold);
    }

    public LogCluster fullSearch(String[] tokens, double similarityThreshold) {
        List<String> clusterIds = this.getClustersIdsForTokenLen(tokens.length);
        return this.bestMatch(clusterIds, tokens, similarityThreshold, true);
    }

    public List<String> getClustersIdsForTokenLen(int tokenLen) {
        List<String> clusterIds = new ArrayList<>();
        Node curNode = this.root.children.get(String.valueOf(tokenLen));
        if (curNode == null) {
            return clusterIds;
        }
        // travel all nodes for the cluster
        appendClustersRecursive(curNode, clusterIds);
        return clusterIds;
    }

    private void appendClustersRecursive(Node node, List<String> clusterIds) {
        clusterIds.addAll(node.getClusterIds());
        for (Node child : node.getChildren().values()) {
            appendClustersRecursive(child, clusterIds);
        }
    }

    public LogCluster aggregate(String message) {
        String[] tokens = genTokens(message);
        if (this.maxTokens > 0 && tokens.length >= maxTokens) {
            tokens = Arrays.copyOf(tokens, this.maxTokens);
        }
        LogCluster matchCluster = this.treeSearch(tokens, this.similarityThreshold, false);
        if (matchCluster == null) {
            matchCluster = new LogCluster("", tokens);
            matchCluster.setState(LogClusterState.CLUSTER_CREATED);
        } else {
            String[] newTokens = this.mergeTemplates(tokens, matchCluster.getTokens());
            if (Arrays.equals(newTokens, matchCluster.getTokens())) {
                matchCluster.setState(LogClusterState.NONE);
            } else {
                matchCluster.setState(LogClusterState.CLUSTER_CHANGED);
                matchCluster.setTokens(newTokens);
            }
        }
        return matchCluster;
    }

    public LogCluster aggregateAndUpdate(String message) {
        String[] tokens = genTokens(message);
        if (this.maxTokens > 0 && tokens.length >= maxTokens) {
            tokens = Arrays.copyOf(tokens, this.maxTokens);
        }
        LogCluster matchCluster = this.treeSearch(tokens, this.similarityThreshold, false);
        if (matchCluster == null) {
            String id = this.idGenerator.next();
            matchCluster = new LogCluster(id, tokens);

            this.idToCluster.put(id, matchCluster);
            this.addLogClusterToPrefixTree(matchCluster);

            matchCluster.setState(LogClusterState.CLUSTER_CREATED);
        } else {
            String[] newTokens = this.mergeTemplates(tokens, matchCluster.getTokens());
            if (Arrays.equals(newTokens, matchCluster.getTokens())) {
                matchCluster.setState(LogClusterState.NONE);
            } else {
                matchCluster.setTokens(newTokens);
                matchCluster.setState(LogClusterState.CLUSTER_CHANGED);
            }
        }
        return matchCluster;
    }

    public LogCluster treeSearch(String[] tokens, double similarityThreshold, boolean includeParam) {
        int tokenLen = tokens.length;
        Node curNode = this.root.children.get(String.valueOf(tokenLen));

        if (curNode == null) return null; // no template
        if (tokenLen == 0) return this.idToCluster.get(curNode.getClusterIds().get(0));

        // find the leaf node for this log - a path of nodes matching the first N tokens (N = tree depth)
        int depth = 1;
        for (String token : tokens) {
            if (depth >= this.maxNodeDepth || depth >= tokenLen) { // the last node is cluster id list
                break;
            }
            Map<String, Node> children = curNode.getChildren();
            curNode = children.get(token);
            if (curNode == null) { // maybe holder like *(wildcard)
                curNode = children.get(this.paramHolder);
            }
            if (curNode == null) {
                return null;
            }
            depth += 1;
        }
        // get beat match among all clusters with same prefix, or None if no match is above similarity threshold
        return this.bestMatch(curNode.getClusterIds(), tokens, similarityThreshold, includeParam);
    }

    /**
     * Get the best log cluster in the same template
     *
     * @param clusterIds
     * @param tokens
     * @param includeParam
     * @return
     */
    public LogCluster bestMatch(List<String> clusterIds, String[] tokens, double similarityThreshold, boolean includeParam) {
        LogCluster candidate = null;
        Similarity max = new Similarity();

        for (String clusterId : clusterIds) {
            LogCluster logCluster = this.idToCluster.get(clusterId);
            if (logCluster == null) continue;

            Similarity similarity = this.computingSimilarity(logCluster.getTokens(), tokens, includeParam);
            if (similarity.getRatio() > max.getRatio() ||
                    (similarity.getRatio() == max.getRatio() && similarity.getParamCount() > max.getParamCount())) {
                max = similarity;
                candidate = logCluster;
            }
        }
        return max.getRatio() >= similarityThreshold ? candidate : null;
    }

    /**
     * add log cluster to prefix tree
     *
     * @param cluster
     */
    public void addLogClusterToPrefixTree(LogCluster cluster) {
        int tokenLen = cluster.getTokens().length;
        String firstLevel = Integer.toString(tokenLen);

        this.root.getChildren().putIfAbsent(firstLevel, new Node());
        Node curNode = this.root.children.get(firstLevel);
        if (tokenLen == 0) {
            curNode.getClusterIds().add(cluster.getId());
            return;
        }

        int depth = 1;
        for (String token : cluster.getTokens()) {
            // if at max depth or this is last token in template - add current log cluster to the leaf node
            if (depth >= this.maxNodeDepth || depth == tokenLen) {
                Set<String> clusterIds = new HashSet<>();
                for (String clusterId : curNode.getClusterIds()) {
                    if (idToCluster.contains(clusterId)) {
                        clusterIds.add(clusterId);
                    }
                }
                clusterIds.add(cluster.getId());
                curNode.setClusterIds(new ArrayList<>(clusterIds));
                break;
            }

            // if token not matched in this layer of existing tree.
            if (!curNode.getChildren().containsKey(token)) {
                if (this.parametrizeNumericTokens && hasNumbers(token)) {
                    if (!curNode.getChildren().containsKey(this.paramHolder)) {
                        Node newNode = new Node();
                        curNode.getChildren().put(paramHolder, newNode);
                        curNode = newNode;
                    } else {
                        curNode = curNode.getChildren().get(this.paramHolder);
                    }
                } else {
                    if (curNode.getChildren().containsKey(this.paramHolder)) {
                        if (curNode.getChildren().size() < this.maxChildren) {
                            Node newNode = new Node();
                            curNode.getChildren().put(token, newNode);
                            curNode = newNode;
                        } else {
                            curNode = curNode.getChildren().get(this.paramHolder);
                        }
                    } else {
                        if (curNode.getChildren().size() + 1 < this.maxChildren) {
                            Node newNode = new Node();
                            curNode.getChildren().put(token, newNode);
                            curNode = newNode;
                        } else if (curNode.getChildren().size() + 1 == this.maxChildren) {
                            // add parameter holder to meet the condition that matches any tokens, next token will jump to step 3
                            Node newNode = new Node();
                            curNode.getChildren().put(this.paramHolder, newNode);
                            curNode = newNode;
                        } else { // step 3
                            curNode = curNode.getChildren().get(this.paramHolder);
                        }
                    }
                }
            } else { // if the token is matched
                curNode = curNode.getChildren().get(token);
            }
            depth += 1;
        }
    }

    /**
     * Split message into tokens by space or extra delimiters.
     *
     * @param message
     * @return
     */
    public String[] genTokens(String message) {
        message = message.trim();
        for (String delimiter : this.extraDelimiters) {
            message = message.replaceAll(delimiter, " ");
        }
        return message.split("\\s+");
    }

    /**
     * Computing similarity for s1 and s2.
     *
     * @param s1
     * @param s2
     * @param includeParam
     * @return
     */
    public Similarity computingSimilarity(String[] s1, String[] s2, boolean includeParam) {
        assert s1.length == s2.length : "Can not get similarity for the different length string";
        if (s1.length == 0) {
            return new Similarity(1.0, 0);
        }
        int similarCount = 0, paramCount = 0, tokenLen = s1.length;
        for (int i = 0; i < tokenLen; i++) {
            paramCount += s1[i].equals(this.paramHolder) ? 1 : 0;
            similarCount += s1[i].equals(s2[i]) ? 1 : 0;
        }
        similarCount += includeParam ? paramCount : 0;
        return new Similarity(similarCount * 1.0 / tokenLen, paramCount);
    }

    /**
     * Merge templates
     *
     * @param s1
     * @param s2
     * @return
     */
    public String[] mergeTemplates(String[] s1, String[] s2) {
        assert s1.length == s2.length : "length between s1 and s2 should be the same";
        String[] tokens = new String[s1.length];
        for (int i = 0; i < s1.length; i++) {
            tokens[i] = s1[i].equals(s2[i]) ? s1[i] : this.paramHolder;
        }
        return tokens;
    }

    public boolean hasNumbers(String token) {
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if ('0' <= c && c <= '9') {
                return true;
            }
        }
        return false;
    }

    /**
     * Add logCluster into drain.
     * @param logCluster
     */
    public void addCluster(LogCluster logCluster) {
        this.idToCluster.put(logCluster.getId(), logCluster);
        this.addLogClusterToPrefixTree(logCluster);
    }

    /**
     * Node to log token, needed by build a fast searching tree.
     */
    @Data
    public static class Node {
        /**
         * Next node of current node
         */
        private Map<String, Node> children;
        /**
         * The leaf the node will collect clusterId(LogCluster.id).
         */
        private List<String> clusterIds;

        public Node() {
            this.children = new HashMap<>();
            this.clusterIds = new ArrayList<>();
        }
    }

    @Data
    public static class Similarity {
        /**
         * Similar ratio between s1 and s2
         */
        private double ratio;
        /**
         * The number of the parameter holders
         */
        private int paramCount;

        public Similarity() {
            this.ratio = -1;
            this.paramCount = -1;
        }

        public Similarity(double ratio, int paramCount) {
            this.ratio = ratio;
            this.paramCount = paramCount;
        }
    }
}
