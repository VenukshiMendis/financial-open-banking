/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.wso2.openbanking.accelerator.common.util;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingRuntimeException;
import com.wso2.openbanking.accelerator.common.identity.IdentityConstants;
import net.minidev.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

/**
 * Open Banking common utility class.
 */
public class OpenBankingUtils {

    private static final Log log = LogFactory.getLog(OpenBankingUtils.class);

    /**
     * Method to obtain the Object when the full class path is given.
     *
     * @param classpath full class path
     * @return new object instance
     */
    @Generated(message = "Ignoring since method contains no logics")
    public static Object getClassInstanceFromFQN(String classpath) {

        try {
            return Class.forName(classpath).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            log.error("Class not found: " + classpath.replaceAll("[\r\n]", ""));
            throw new OpenBankingRuntimeException("Cannot find the defined class", e);
        } catch (InstantiationException | InvocationTargetException |
                NoSuchMethodException | IllegalAccessException e) {
            //Throwing a runtime exception since we cannot proceed with invalid objects
            throw new OpenBankingRuntimeException("Defined class" + classpath + "cannot be instantiated.", e);
        }
    }

    /**
     * Extract software_environment (SANDBOX or PRODUCTION) from SSA.
     *
     * @param softwareStatement software statement (jwt) extracted from request payload
     * @return software_environment
     * @throws ParseException  if an error occurs while parsing the software statement
     */
    public static String getSoftwareEnvironmentFromSSA(String softwareStatement) throws ParseException {

        if (StringUtils.isEmpty(softwareStatement)) {
            return IdentityConstants.PRODUCTION;
        }

        final JSONObject softwareStatementBody = JWTUtils.decodeRequestJWT(softwareStatement,
                OpenBankingConstants.JWT_BODY);
        // Retrieve the SSA property name used for software environment identification
        final String sandboxEnvIdentificationPropertyName = OpenBankingConfigParser.getInstance()
                .getSoftwareEnvIdentificationSSAPropertyName();
        // Retrieve the expected value for the sandbox environment
        final String sandboxEnvIdentificationValue = OpenBankingConfigParser.getInstance()
                .getSoftwareEnvIdentificationSSAPropertyValueForSandbox();
        return sandboxEnvIdentificationValue.equalsIgnoreCase(softwareStatementBody
                .getAsString(sandboxEnvIdentificationPropertyName))
                ? IdentityConstants.SANDBOX
                : IdentityConstants.PRODUCTION;
    }

    /**
     * Method to obtain boolean value for check if the Dispute Resolution Data is publishable.
     *
     * @param statusCode for dispute data
     * @return boolean
     */
    public static boolean isPublishableDisputeData(int statusCode) {

        if (statusCode < 400 &&
                OpenBankingConfigParser.getInstance().isNonErrorDisputeDataPublishingEnabled()) {
            return  true;
        }

        return statusCode >= 400;
    }

    /**
     * Method to reduce string length.
     *
     * @param input        Input for dispute data
     * @param maxLength    Max length for dispute data
     * @return String with reduced length
     */
    public static String reduceStringLength(String input, int maxLength) {
        if (StringUtils.isEmpty(input) || input.length() <= maxLength) {
            return input;
        } else {
            return input.substring(0, maxLength);
        }
    }
}
