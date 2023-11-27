package com.oppo.cloud.gpt.drain;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Mask {
    /**
     * Prefix of mask
     */
    private String prefix;
    /**
     * Suffix of mask
     */
    private String suffix;
    /**
     * Mask rules
     */
    private List<MaskRule> rules;

    public Mask(List<MaskRule> rules, String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.rules = rules;
    }

    /**
     * Mask string
     * @param s
     * @return
     */
    public String mask(String s) {
        for (MaskRule rule : this.rules) {
            s = rule.mask(s, prefix, suffix);
        }
        return s;
    }
}
