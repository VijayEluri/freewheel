/*
 * Copyright (c) 2012 SixRQ Ltd.
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

package org.freewheelschedule.freewheel.remoteworker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.freewheelschedule.freewheel.common.message.JobInitiationMessage;
import org.freewheelschedule.freewheel.common.message.JobResponseMessage;
import org.freewheelschedule.freewheel.common.model.Status;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static org.freewheelschedule.freewheel.common.message.Conversation.*;

public class CommandLineExecution implements Execution {
    private static Log log = LogFactory.getLog(CommandLineExecution.class);

    private JobInitiationMessage command;
    private int remotePort;

    @Override
    public void run() {

        String message;
        PrintWriter stdoutOutput = null;
        PrintWriter stderrOutput = null;
        JobResponseMessage responseMessage = new JobResponseMessage();

        ObjectMapper mapper = new ObjectMapper();

        String hostname;
        try {
            hostname = (InetAddress.getLocalHost()).getCanonicalHostName();
        } catch (UnknownHostException e1) {
            log.error("Unable to determine hostname", e1);
            return;
        }

        log.info("Running command " + command);

        try {
            Socket remoteWorker = new Socket(hostname, remotePort);

            PrintWriter speak = new PrintWriter(remoteWorker.getOutputStream(), true);
            BufferedReader result = new BufferedReader(new InputStreamReader(remoteWorker.getInputStream()));

            String response = result.readLine();
            if (response.equals(HELO)) {
                speak.print(HELO + " " + hostname + "\r\n");
                speak.flush();
                responseMessage.setUid(command.getUid());
                responseMessage.setStatus(Status.STARTED);
                responseMessage.setMessage(STARTED + " " + command.getUid());
                speak.print(mapper.writeValueAsString(responseMessage) + "\r\n");
                speak.flush();
            } else {
                log.error("Unexpected response from ControlServer");
                return;
            }

            result.close();
            speak.close();
            remoteWorker.close();

            Process process = Runtime.getRuntime().exec(command.getCommand());

            if (command.getStdout() != null) {
                stdoutOutput = new PrintWriter(new FileOutputStream(command.getStdout(), command.getAppendStdout()));
            }
            if (command.getStderr() != null) {
                stderrOutput = new PrintWriter(new FileOutputStream(command.getStderr(), command.getAppendStderr()));
            }
            // getInputStream() returns the stdout of the process that ran
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((message = stdOut.readLine()) != null) {
                log.info("stdout: " + message);
                if (stdoutOutput != null) {
                    stdoutOutput.write(message + "\n");
                }
            }
            while ((message = stdErr.readLine()) != null) {
                log.info("stderr: " + message);
                if (stderrOutput != null) {
                    stderrOutput.write(message + "\n");
                }
            }
            if (stderrOutput != null) {
                stderrOutput.close();
                stderrOutput = null;
            }
            if (stdoutOutput != null) {
                stdoutOutput.close();
                stdoutOutput = null;
            }

            remoteWorker = new Socket(hostname, remotePort);

            speak = new PrintWriter(remoteWorker.getOutputStream(), true);
            result = new BufferedReader(new InputStreamReader(remoteWorker.getInputStream()));

            response = result.readLine();
            if (response.equals(HELO)) {
                speak.print(HELO + " " + hostname + "\r\n");
                speak.flush();
                responseMessage.setUid(command.getUid());
                responseMessage.setStatus(process.exitValue() == 0 ? Status.SUCCESS : Status.FAILURE);
                responseMessage.setExitValue(process.exitValue());
                responseMessage.setMessage(COMPLETE + " " + command.getUid());
                speak.print(mapper.writeValueAsString(responseMessage) + "\r\n");
                speak.flush();
            } else {
                log.error("Unexpected response from ControlServer");
                return;
            }

            result.close();
            speak.close();
            remoteWorker.close();

        } catch (IOException e) {
            log.error("Execution failed", e);
        } finally {
            if (stderrOutput != null) {
                stderrOutput.close();
            }
            if (stdoutOutput != null) {
                stdoutOutput.close();
            }
        }

    }

    @Override
    public void setCommand(JobInitiationMessage command) {
        this.command = command;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
}
