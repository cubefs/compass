package com.oppo.cloud.gpt.drain;

import lombok.Data;

@Data
public class MaskRule {
    /**
     * Replacement for the regex.
     */
    private String maskWith;
    /**
     * Express for matching.
     */
    private String regex;

    public MaskRule(String regex, String maskWith) {
        this.regex = regex;
        this.maskWith = maskWith;
    }

    /**
     * Replace string with mask according to regex.
     * @param s
     * @param prefix
     * @param suffix
     * @return
     */
    public String mask(String s, String prefix, String suffix) {
        return s.replaceAll(regex, prefix + maskWith + suffix);
    }
}
