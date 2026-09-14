package cn.huntercat.lieshoucloudpro.auth.feign;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

import cn.huntercat.lieshou.framework.common.api.BaseException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import java.nio.charset.StandardCharsets;

/**
 * UserAuthClient 的 Feign 错误解码.
 *
 * <p>把 user 服务 4xx 业务错误（响应体 {@code {"error":"SEND_TOO_FREQUENT"}}）解码成 {@link BaseException}
 * （业务错误码透传），而非默认的 {@code FeignException}——否则框架层 {@code AuthService} 会把它当依赖故障 误报成
 * 503「服务不可用」。典型场景：短信验证码发送过于频繁（SEND_TOO_FREQUENT）。
 */
public class UserAuthErrorDecoder {

  @Bean
  public ErrorDecoder errorDecoder() {
    return new ErrorDecoder() {
      private final ErrorDecoder delegate = new ErrorDecoder.Default();

      @Override
      public Exception decode(String methodKey, Response response) {
        if (response.status() >= 400 && response.status() < 500) {
          String code = extractErrorCode(response);
          if (code != null && !code.isBlank()) {
            return new BaseException(code, HttpStatus.valueOf(response.status()), "上游业务错误：" + code);
          }
        }
        return delegate.decode(methodKey, response);
      }
    };
  }

  /** 从响应体提取错误码（{"error":"SEND_TOO_FREQUENT"} → SEND_TOO_FREQUENT）。 */
  private String extractErrorCode(Response response) {
    try {
      byte[] bytes = Util.toByteArray(response.body().asInputStream());
      String body = new String(bytes, StandardCharsets.UTF_8);
      int i = body.indexOf("\"error\"");
      int colon = i >= 0 ? body.indexOf(':', i + 7) : -1;
      int q1 = colon >= 0 ? body.indexOf('"', colon + 1) : -1;
      int q2 = q1 >= 0 ? body.indexOf('"', q1 + 1) : -1;
      if (q1 > 0 && q2 > q1) {
        return body.substring(q1 + 1, q2);
      }
    } catch (Exception ignored) {
      // 解析失败则回退默认解码
    }
    return null;
  }
}
