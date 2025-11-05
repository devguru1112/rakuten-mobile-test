package com.rakuten.mobile.server.web.dto.question;

import com.rakuten.mobile.server.domain.OptionChoice;

import java.util.UUID;

public record OptionRes(UUID id, String label, String value, int position) {
    public static OptionRes from(OptionChoice o) {
        return new OptionRes(o.getId(), o.getLabel(), o.getValue(), o.getPosition());
    }
}
