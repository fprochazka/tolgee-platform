package io.tolgee.service.security

import io.tolgee.constants.Message
import io.tolgee.exceptions.AuthenticationException
import io.tolgee.model.UserAccount
import io.tolgee.service.TenantService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserCredentialsService(
  private val passwordEncoder: PasswordEncoder,
  private val tenantService: TenantService,
) {
  @set:Autowired
  lateinit var userAccountService: UserAccountService

  fun checkUserCredentials(
    username: String,
    password: String,
  ): UserAccount {
    val userAccount = userAccountService.findActive(username)
    if (userAccount == null) {
      tenantService.checkSsoNotRequired(username)
      throw AuthenticationException(Message.BAD_CREDENTIALS)
    }

    tenantService.checkSsoNotRequiredOrAuthProviderChangeActive(userAccount)

    if (userAccount.accountType == UserAccount.AccountType.MANAGED) {
      throw AuthenticationException(Message.OPERATION_UNAVAILABLE_FOR_ACCOUNT_TYPE)
    }

    checkNativeUserCredentials(userAccount, password)
    return userAccount
  }

  fun checkUserCredentials(
    user: UserAccount,
    password: String,
  ) {
    checkNativeUserCredentials(user, password)
  }

  private fun checkNativeUserCredentials(
    user: UserAccount,
    password: String,
  ) {
    if (!passwordEncoder.matches(password, user.password)) {
      throw AuthenticationException(Message.BAD_CREDENTIALS)
    }
  }
}
