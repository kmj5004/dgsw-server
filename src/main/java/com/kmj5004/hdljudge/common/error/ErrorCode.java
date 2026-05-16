package com.kmj5004.hdljudge.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {


    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "요청이 올바르지 않습니다."),
    MALFORMED_BODY(HttpStatus.BAD_REQUEST, "MALFORMED_BODY", "요청 본문을 해석할 수 없습니다."),


    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "토큰이 만료되었습니다."),


    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없습니다."),


    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "리소스를 찾을 수 없습니다."),
    CHALLENGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHALLENGE_NOT_FOUND", "챌린지를 찾을 수 없습니다."),
    SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBMISSION_NOT_FOUND", "제출을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),


    CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "리소스 상태가 요청과 충돌합니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 사용 중인 이메일입니다."),
    SLUG_ALREADY_EXISTS(HttpStatus.CONFLICT, "SLUG_ALREADY_EXISTS", "이미 사용 중인 챌린지 식별자입니다."),


    JUDGE_QUEUE_FULL(HttpStatus.TOO_MANY_REQUESTS, "JUDGE_QUEUE_FULL", "채점 큐가 가득 찼습니다. 잠시 후 다시 시도해주세요."),


    INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL", "서버 내부 오류가 발생했습니다."),
    JUDGE_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "JUDGE_FAILURE", "채점 실행 중 오류가 발생했습니다."),
    SYNTHESIS_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "SYNTHESIS_FAILURE", "합성 실행 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String code, String defaultMessage) {
        this.status = status;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
