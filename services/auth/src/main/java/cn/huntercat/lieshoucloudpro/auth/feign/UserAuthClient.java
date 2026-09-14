package cn.huntercat.lieshoucloudpro.auth.feign;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.cloud.openfeign.FeignClient;

import cn.huntercat.lieshou.framework.auth.UserAuthPort;
import cn.huntercat.lieshou.framework.common.dto.UserAuthView;
import java.util.Map;

/**
 * auth-service 调 user-service 拉 user 视图（含 passwordHash）.
 *
 * <p><b>薄壳装配</b>：业务契约在 {@link UserAuthPort}（LieShou-framework，上游同源唯一）； 本接口仅补 Feign 的 HTTP 映射注解 +
 * {@code @FeignClient}。
 */
@FeignClient(
    name = "lieshoucloud-user",
    path = "/api/users",
    configuration = UserAuthErrorDecoder.class)
public interface UserAuthClient extends UserAuthPort {

  @Override
  @GetMapping("/auth/by-tenant/{tenantCode}/{username}")
  UserAuthView findByTenantAndUsername(
      @PathVariable String tenantCode, @PathVariable String username);

  @Override
  @GetMapping("/auth/tenant-options")
  java.util.List<java.util.Map<String, Object>> tenantOptions(@RequestParam String username);

  @Override
  @PostMapping("/{id}/login-marker")
  void markLastLogin(@PathVariable Long id);

  @Override
  @PostMapping("/verification/send")
  void sendVerificationCode(@RequestBody Map<String, String> body);

  @Override
  @PostMapping("/verification/verify")
  void verifyVerificationCode(@RequestBody Map<String, String> body);

  @Override
  @GetMapping("/auth/by-tenant-phone/{tenantCode}/{phone}")
  UserAuthView findByTenantAndPhone(@PathVariable String tenantCode, @PathVariable String phone);

  @Override
  @GetMapping("/auth/by-email/{email}")
  UserAuthView findByEmail(@PathVariable String email);

  @Override
  @PostMapping("")
  Map<String, Object> createUser(@RequestBody Map<String, String> body);

  @Override
  @PutMapping("/{id}")
  void updateUserPassword(@PathVariable Long id, @RequestBody Map<String, String> body);
}
