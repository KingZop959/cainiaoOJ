<template>
  <div id="userLoginView">
    <div style="text-align: center; margin-bottom: 20px">
      <a-radio-group v-model="mode" type="button" size="large">
        <a-radio value="login">登录</a-radio>
        <a-radio value="register">注册</a-radio>
      </a-radio-group>
    </div>

    <h2 style="margin-bottom: 16px; text-align: center">
      {{ mode === "login" ? "用户登录" : "用户注册" }}
    </h2>

    <a-form
      style="max-width: 480px; margin: 0 auto"
      label-align="left"
      auto-label-width
      :model="mode === 'login' ? loginForm : registerForm"
      @submit="handleSubmit"
    >
      <a-form-item field="userAccount" label="账号">
        <a-input
          :value="
            mode === 'login' ? loginForm.userAccount : registerForm.userAccount
          "
          @update:model-value="updateUserAccount"
          placeholder="请输入账号"
        />
      </a-form-item>

      <a-form-item field="userPassword" tooltip="密码不少于 8 位" label="密码">
        <a-input-password
          :value="
            mode === 'login'
              ? loginForm.userPassword
              : registerForm.userPassword
          "
          @update:model-value="updateUserPassword"
          placeholder="请输入密码"
        />
      </a-form-item>

      <!-- 注册模式下显示确认密码字段 -->
      <a-form-item
        v-if="mode === 'register'"
        field="checkPassword"
        tooltip="请再次输入密码进行确认"
        label="确认密码"
      >
        <a-input-password
          v-model="registerForm.checkPassword"
          placeholder="请再次输入密码"
        />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 120px">
          {{ mode === "login" ? "登录" : "注册" }}
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import {
  UserControllerService,
  UserLoginRequest,
  UserRegisterRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";

/**
 * 模式：登录或注册
 */
const mode = ref("login");

/**
 * 登录表单信息
 */
const loginForm = reactive({
  userAccount: "",
  userPassword: "",
} as UserLoginRequest);

/**
 * 注册表单信息
 */
const registerForm = reactive({
  userAccount: "",
  userPassword: "",
  checkPassword: "",
} as UserRegisterRequest);

const router = useRouter();
const store = useStore();

/**
 * 更新用户账号
 */
const updateUserAccount = (value: string) => {
  if (mode.value === "login") {
    loginForm.userAccount = value;
  } else {
    registerForm.userAccount = value;
  }
};

/**
 * 更新用户密码
 */
const updateUserPassword = (value: string) => {
  if (mode.value === "login") {
    loginForm.userPassword = value;
  } else {
    registerForm.userPassword = value;
  }
};

/**
 * 处理登录
 */
const handleLogin = async () => {
  try {
    const res = await UserControllerService.userLoginUsingPost(loginForm);
    if (res.code === 0) {
      message.success("登录成功！");
      await store.dispatch("user/getLoginUser");
      router.push({
        path: "/",
        replace: true,
      });
    } else {
      message.error("登录失败，" + res.message);
    }
  } catch (error) {
    message.error("登录失败，请检查网络连接");
  }
};

/**
 * 处理注册
 */
const handleRegister = async () => {
  // 前端验证
  if (
    !registerForm.userAccount ||
    !registerForm.userPassword ||
    !registerForm.checkPassword
  ) {
    message.error("请填写完整信息");
    return;
  }

  if (registerForm.userPassword !== registerForm.checkPassword) {
    message.error("两次输入的密码不一致");
    return;
  }

  if (registerForm.userPassword.length < 8) {
    message.error("密码长度不能少于8位");
    return;
  }

  try {
    const res = await UserControllerService.userRegisterUsingPost(registerForm);
    if (res.code === 0) {
      message.success("注册成功！请登录");
      // 注册成功后切换到登录模式，并预填账号
      mode.value = "login";
      loginForm.userAccount = registerForm.userAccount;
      loginForm.userPassword = registerForm.userPassword;
      registerForm.userAccount = "";
      registerForm.userPassword = "";
      registerForm.checkPassword = "";
    } else {
      message.error("注册失败，" + res.message);
    }
  } catch (error) {
    message.error("注册失败，请检查网络连接");
  }
};

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (mode.value === "login") {
    await handleLogin();
  } else {
    await handleRegister();
  }
};
</script>
