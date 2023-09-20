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

package com.oppo.cloud.syncer.azkaban;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.util.zip.GZIPInputStream;

public class TestAzKaBanProjectFlowDecode {

    @Test
    public void testFlowDecode() {
        String SELECT_ALL_PROJECT_FLOWS =
                "SELECT project_id, version, flow_id, modified_time, encoding_type, json " +
                        "FROM project_flows WHERE project_id=1 AND version=2";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection =
                    DriverManager.getConnection("jdbc:mysql://localhost:3306/azkaban", "root", "Root@666");
            PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_PROJECT_FLOWS);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String flowId = rs.getString(3);
                int encodingType = rs.getInt(5);
                byte[] dataBytes = rs.getBytes(6);
                ByteArrayInputStream byteInputStream = new ByteArrayInputStream(dataBytes);
                GZIPInputStream gzipInputStream = new GZIPInputStream(byteInputStream);

                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                IOUtils.copy(gzipInputStream, byteOutputStream);
                String s = new String(byteOutputStream.toByteArray(), "UTF-8");
                System.out.println(s);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
