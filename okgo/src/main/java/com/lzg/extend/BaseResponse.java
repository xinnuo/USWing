/*
 * Copyright 2016 jeasonlzy(廖子尧)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lzg.extend;

import java.io.Serializable;

public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 5213230387175987834L;

    public int code;
    public String info;
    public int msgcode;
    public String msg;
    public T object;

    @Override
    public String toString() {
        return "BaseResponse{\n"
                + "\tcode=" + code + "\n"
                + "\tinfo='" + info + "\'\n"
                + "\tmsgcode='" + msgcode + "\'\n"
                + "\tmsg='" + msg + "\'\n"
                + "\tobject=" + object + "\n" + '}';
    }
}
