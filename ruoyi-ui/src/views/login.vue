<template>
  <div class="login">
    <section class="brand-panel">
      <div class="brand-mark">家</div>
      <h1>家政到家服务平台</h1>
      <p>需求审核、阿姨推荐、面试邀约、内容消息统一管理。</p>
    </section>

    <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
      <div class="title-box">
        <h3 class="title">账号登录</h3>
        <p class="subtitle">请输入管理员用户名和密码</p>
      </div>

      <el-form-item prop="username">
        <el-input v-model.trim="loginForm.username" type="text" size="large" auto-complete="off" placeholder="请输入用户名">
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>

      <el-form-item prop="password">
        <el-input
          v-model.trim="loginForm.password"
          type="password"
          size="large"
          auto-complete="off"
          show-password
          placeholder="请输入密码"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>

      <el-form-item style="width: 100%">
        <el-button :loading="loading" size="large" type="primary" style="width: 100%" @click.prevent="handleLogin">
          <span v-if="!loading">登录</span>
          <span v-else>登录中...</span>
        </el-button>
      </el-form-item>
    </el-form>

    <div class="el-login-footer">
      <span>家政到家服务管理后台</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/modules/user';
import { LoginData } from '@/api/types';
import { to } from 'await-to-js';

const loginForm = ref<LoginData>({
  tenantId: '000000',
  username: '',
  password: '',
  rememberMe: false,
  code: '',
  uuid: ''
} as LoginData);

const loginRules: ElFormRules = {
  username: [{ required: true, trigger: 'blur', message: '请输入用户名' }],
  password: [{ required: true, trigger: 'blur', message: '请输入密码' }]
};

const userStore = useUserStore();
const router = useRouter();
const loading = ref(false);
const redirect = ref('/');
const loginRef = ref<ElFormInstance>();

watch(
  () => router.currentRoute.value,
  (newRoute: any) => {
    redirect.value = newRoute.query && newRoute.query.redirect && decodeURIComponent(newRoute.query.redirect);
  },
  { immediate: true }
);

const handleLogin = () => {
  loginRef.value?.validate(async (valid: boolean, fields: any) => {
    if (valid) {
      loading.value = true;
      localStorage.removeItem('tenantId');
      localStorage.removeItem('username');
      localStorage.removeItem('password');
      localStorage.removeItem('rememberMe');
      loginForm.value.tenantId = '000000';
      loginForm.value.rememberMe = false;
      const [err] = await to(userStore.login(loginForm.value));
      loading.value = false;
      if (!err) {
        await router.push(redirect.value || '/');
      }
    } else {
      console.log('error submit!', fields);
    }
  });
};
</script>

<style lang="scss" scoped>
.login {
  display: flex;
  min-height: 100%;
  align-items: center;
  justify-content: center;
  gap: 48px;
  padding: 48px;
  background:
    radial-gradient(circle at 12% 18%, rgba(232, 77, 100, 0.16), transparent 28%),
    radial-gradient(circle at 82% 20%, rgba(64, 112, 84, 0.14), transparent 30%),
    linear-gradient(135deg, #f7f6ef 0%, #edf2ee 52%, #f8f2f4 100%);
}

.brand-panel {
  width: min(460px, 38vw);
  color: #20252b;

  .brand-mark {
    display: flex;
    width: 64px;
    height: 64px;
    align-items: center;
    justify-content: center;
    border-radius: 18px;
    background: #20252b;
    color: #fff;
    font-size: 28px;
    font-weight: 800;
    box-shadow: 0 18px 42px rgba(32, 37, 43, 0.18);
  }

  h1 {
    margin: 24px 0 12px;
    font-size: 38px;
    line-height: 1.2;
    font-weight: 800;
  }

  p {
    margin: 0;
    color: #68717a;
    font-size: 16px;
    line-height: 1.8;
  }
}

.title-box {
  margin-bottom: 28px;

  .title {
    margin: 0;
    color: var(--el-text-color-primary);
    font-size: 26px;
    font-weight: 800;
    letter-spacing: 0.5px;
  }

  .subtitle {
    margin: 8px 0 0;
    color: #7b8490;
    font-size: 14px;
  }
}

.login-form {
  z-index: 1;
  width: min(420px, 92vw);
  padding: 36px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(32, 37, 43, 0.12);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);

  .el-input {
    height: 46px;

    input {
      height: 46px;
    }
  }

  .input-icon {
    width: 14px;
    height: 45px;
    margin-left: 0;
  }
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  background-color: rgba(255, 255, 255, 0.9);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(232, 77, 100, 0.18);
}

.login-form :deep(.el-button--primary) {
  height: 46px;
  border-color: #e84d64;
  border-radius: 12px;
  background: #e84d64;
  box-shadow: 0 12px 28px rgba(232, 77, 100, 0.22);
}

.el-login-footer {
  position: fixed;
  bottom: 0;
  width: 100%;
  height: 40px;
  color: rgba(32, 37, 43, 0.48);
  font-size: 12px;
  line-height: 40px;
  text-align: center;
  letter-spacing: 1px;
}

@media (max-width: 960px) {
  .login {
    flex-direction: column;
    align-items: stretch;
  }

  .brand-panel {
    width: auto;
    text-align: center;

    .brand-mark {
      margin: 0 auto;
    }
  }

  .login-form {
    margin: 0 auto;
  }
}

:global(html.dark) {
  .login-form {
    border-color: rgba(148, 163, 184, 0.2);
    background: rgba(17, 24, 39, 0.9);
  }

  .login-form :deep(.el-input__wrapper) {
    background-color: rgba(17, 24, 39, 0.7);
  }

  .brand-panel,
  .brand-panel p {
    color: #e5e7eb;
  }

  .el-login-footer {
    color: rgba(226, 232, 240, 0.65);
  }
}
</style>
