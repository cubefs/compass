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

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleIdGenerator implements IdGenerator {
    private AtomicInteger counter;

    public SimpleIdGenerator() {
        this.counter = new AtomicInteger(0);
    }

    public String next() {
        return String.valueOf(this.counter.incrementAndGet());
    }
}
