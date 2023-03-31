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

package com.oppo.cloud.portal.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * md5 encryption utils
 */
public class EncryptionUtils {

    private EncryptionUtils() {
        throw new UnsupportedOperationException("Construct EncryptionUtils");
    }

    /**
     * @param rawStr raw string
     * @return md5(rawStr)
     */
    public static String getMd5(String rawStr) {
        return DigestUtils.md5Hex(null == rawStr ? StringUtils.EMPTY : rawStr);
    }

}
