/**
 * Copyright (C) 2013 Open WhisperSystems
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.pushserver.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;


public class ApnConfiguration {

    @NotEmpty
    @JsonProperty
    private String pushCertificate;

    @NotEmpty
    @JsonProperty
    private String pushKey;

    @NotEmpty
    @JsonProperty
    private String voipCertificate;

    @NotEmpty
    @JsonProperty
    private String voipKey;

    @JsonProperty
    private boolean feedback = true;

    @JsonProperty
    private boolean sandbox;

    public String getPushCertificate() {
        return pushCertificate;
    }

    public String getPushKey() {
        return pushKey;
    }

    public boolean isFeedbackEnabled() {
        return feedback;
    }

    public boolean isSandboxEnabled() {
        return sandbox;
    }

    public String getVoipCertificate() {
        return voipCertificate;
    }

    public String getVoipKey() {
        return voipKey;
    }

}
