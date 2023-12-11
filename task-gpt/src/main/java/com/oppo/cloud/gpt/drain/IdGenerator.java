/*
 * SPDX-License-Identifier: MIT
 * This file implements the Drain algorithm for log parsing.
 * Based on https://github.com/logpai/logparser/blob/master/logparser/Drain/Drain.py by LogPAI team
 */

package com.oppo.cloud.gpt.drain;

public interface IdGenerator {
    String next();
}
