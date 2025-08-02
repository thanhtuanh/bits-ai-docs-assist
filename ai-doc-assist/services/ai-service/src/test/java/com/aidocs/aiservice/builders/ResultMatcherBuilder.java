package com.aidocs.aiservice.builders;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class ResultMatcherBuilder {
    private final List<ResultMatcher> matchers = new ArrayList<>();

    public static ResultMatcherBuilder expect() {
        return new ResultMatcherBuilder();
    }

    // Status mit explizitem HttpStatus
    public ResultMatcherBuilder withStatus(HttpStatus httpStatus) {
        switch (httpStatus) {
            case OK -> matchers.add(MockMvcResultMatchers.status().isOk());
            case BAD_REQUEST -> matchers.add(MockMvcResultMatchers.status().isBadRequest());
            case UNAUTHORIZED -> matchers.add(MockMvcResultMatchers.status().isUnauthorized());
            case FORBIDDEN -> matchers.add(MockMvcResultMatchers.status().isForbidden());
            case NOT_FOUND -> matchers.add(MockMvcResultMatchers.status().isNotFound());
            case METHOD_NOT_ALLOWED -> matchers.add(MockMvcResultMatchers.status().isMethodNotAllowed());
            case UNSUPPORTED_MEDIA_TYPE -> matchers.add(MockMvcResultMatchers.status().isUnsupportedMediaType());
            case INTERNAL_SERVER_ERROR -> matchers.add(MockMvcResultMatchers.status().isInternalServerError());
            default -> matchers.add(MockMvcResultMatchers.status().is(httpStatus.value()));
        }
        return this;
    }

    // Überladung: status(HttpStatus)
    public ResultMatcherBuilder status(HttpStatus httpStatus) {
        return withStatus(httpStatus);
    }

    // NEU: status() ohne Parameter -> Default OK
    public ResultMatcherBuilder status() {
        return withStatus(HttpStatus.OK);
    }

    // Convenience-Methoden
    public ResultMatcherBuilder statusOk() {
        return withStatus(HttpStatus.OK);
    }

    public ResultMatcherBuilder statusBadRequest() {
        return withStatus(HttpStatus.BAD_REQUEST);
    }

    public ResultMatcherBuilder statusUnsupportedMediaType() {
        return withStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // Content-Type JSON
    public ResultMatcherBuilder jsonContent() {
        matchers.add(content().contentType(MediaType.APPLICATION_JSON));
        return this;
    }

    // Beispiel für validierte API-Response
    public ResultMatcherBuilder validAnalysisResponse() {
        matchers.add(jsonPath("$.summary").exists());
        matchers.add(jsonPath("$.keywords").exists());
        matchers.add(jsonPath("$.suggestedComponents").exists());
        matchers.add(jsonPath("$.summary").isString());
        matchers.add(jsonPath("$.keywords").isString());
        matchers.add(jsonPath("$.suggestedComponents").isString());
        return this;
    }

    public ResultMatcherBuilder errorResponse(String errorMessage) {
        matchers.add(jsonPath("$.error").value(errorMessage));
        return this;
    }

    public ResultMatcherBuilder healthResponse() {
        matchers.add(jsonPath("$.status").value("UP"));
        matchers.add(jsonPath("$.service").value("ai-service"));
        matchers.add(jsonPath("$.openai").exists());
        matchers.add(jsonPath("$.timestamp").exists());
        return this;
    }

    public ResultMatcherBuilder infoResponse() {
        matchers.add(jsonPath("$.service").value("AI Analysis Service"));
        matchers.add(jsonPath("$.version").value("1.0.0"));
        matchers.add(jsonPath("$.endpoints").exists());
        matchers.add(jsonPath("$.features").exists());
        return this;
    }

    public ResultMatcherBuilder noSecurityHeaders() {
        matchers.add(header().doesNotExist("X-Powered-By"));
        matchers.add(header().doesNotExist("Server"));
        return this;
    }

    public ResultMatcher[] build() {
        return matchers.toArray(new ResultMatcher[0]);
    }
}