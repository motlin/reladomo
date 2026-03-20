/*
 Copyright 2016 Goldman Sachs.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

package com.gs.fw.common.mithra.maven;

import com.gs.fw.common.freyaxml.generator.Logger;
import org.apache.maven.plugin.logging.Log;

public class FreyaMavenLogger implements Logger
{
    private final Log log;

    public FreyaMavenLogger(Log log)
    {
        this.log = log;
    }

    @Override
    public void info(String s)
    {
        log.info(s);
    }

    @Override
    public void info(String s, Throwable t)
    {
        log.info(s, t);
    }

    @Override
    public void warn(String s)
    {
        log.warn(s);
    }

    @Override
    public void warn(String s, Throwable t)
    {
        log.warn(s, t);
    }

    @Override
    public void error(String s)
    {
        log.error(s);
    }

    @Override
    public void error(String s, Throwable t)
    {
        log.error(s, t);
    }

    @Override
    public void debug(String s)
    {
        log.debug(s);
    }

    @Override
    public void debug(String s, Throwable t)
    {
        log.debug(s, t);
    }
}
