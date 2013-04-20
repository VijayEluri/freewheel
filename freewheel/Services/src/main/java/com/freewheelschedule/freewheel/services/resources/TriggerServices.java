/*
 * Copyright (c) 2013 SixRQ Ltd.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.freewheelschedule.freewheel.services.resources;

import com.freewheelschedule.freewheel.api.TriggerList;
import com.freewheelschedule.freewheel.api.TriggerListBuilder;
import org.freewheelschedule.freewheel.common.dao.TriggerDao;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.freewheelschedule.freewheel.common.util.ApplicationContextProvider.getApplicationContext;

@Path("/triggers")
public class TriggerServices {

    TriggerListBuilder triggerListMapper = new TriggerListBuilder();

    @GET
    @Produces(APPLICATION_JSON)
    public TriggerList getTriggerList() throws IOException {
        TriggerDao triggerDao = (TriggerDao) getApplicationContext().getBean("triggerDao");
        return triggerListMapper.build(triggerDao.read(), true);
    }
}
