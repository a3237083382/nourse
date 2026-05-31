<template>
  <div class="navbar" :class="'nav' + navType">
    <hamburger id="hamburger-container" :is-active="appStore.sidebar.opened" class="hamburger-container" @toggle-click="toggleSideBar" />

    <breadcrumb v-if="navType == NavTypeEnum.LEFT" id="breadcrumb-container" class="breadcrumb-container" />
    <top-nav v-if="navType == NavTypeEnum.MIX" id="topmenu-container" class="topmenu-container" />

    <template v-if="navType == NavTypeEnum.TOP">
      <logo v-show="showLogo" :collapse="false"></logo>
      <top-bar id="topbar-container" class="topbar-container" />
    </template>

    <div class="right-menu flex align-center">
      <template v-if="appStore.device !== 'mobile'">
        <el-tooltip :content="proxy.$t('navbar.full')" effect="dark" placement="bottom">
          <screenfull id="screenfull" class="right-menu-item hover-effect" />
        </el-tooltip>
      </template>
      <div class="avatar-container">
        <el-dropdown class="right-menu-item hover-effect" trigger="click" @command="handleCommand">
          <div class="avatar-wrapper">
            <img :src="userStore.avatar" class="user-avatar" />
            <el-icon><caret-bottom /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <router-link to="/user/profile">
                <el-dropdown-item>{{ proxy.$t('navbar.personalCenter') }}</el-dropdown-item>
              </router-link>
              <el-dropdown-item v-if="settingsStore.showSettings" command="setLayout">
                <span>{{ proxy.$t('navbar.layoutSetting') }}</span>
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <span>{{ proxy.$t('navbar.logout') }}</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAppStore } from '@/store/modules/app';
import { useUserStore } from '@/store/modules/user';
import { useSettingsStore } from '@/store/modules/settings';
import router from '@/router';
import { ElMessageBoxOptions } from 'element-plus/es/components/message-box/src/message-box.type';
import { NavTypeEnum } from '@/enums/NavTypeEnum';
import Logo from '@/layout/components/Sidebar/Logo.vue';
import TopBar from './TopBar';

const appStore = useAppStore();
const userStore = useUserStore();
const settingsStore = useSettingsStore();

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const navType = computed(() => settingsStore.navType);
const showLogo = computed(() => settingsStore.sidebarLogo);

const toggleSideBar = () => {
  appStore.toggleSideBar(false);
};

const initTenantList = async () => {};

defineExpose({
  initTenantList
});

const logout = async () => {
  await ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  } as ElMessageBoxOptions);
  userStore.logout().then(() => {
    router.replace({
      path: '/login',
      query: {
        redirect: encodeURIComponent(router.currentRoute.value.fullPath || '/')
      }
    });
    proxy?.$tab.closeAllPage();
  });
};

const emits = defineEmits(['setLayout']);
const setLayout = () => {
  emits('setLayout');
};

const commandMap: { [key: string]: any } = {
  setLayout,
  logout
};
const handleCommand = (command: string) => {
  if (commandMap[command]) {
    commandMap[command]();
  }
};
</script>

<style lang="scss" scoped>
.navbar.navtop {
  .hamburger-container {
    display: none !important;
  }
}

.flex {
  display: flex;
}

.align-center {
  align-items: center;
}

.navbar {
  position: relative;
  display: flex;
  height: 50px;
  align-items: center;
  overflow: hidden;
  box-sizing: border-box;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  box-shadow: none;

  .hamburger-container {
    display: flex;
    height: 100%;
    flex-shrink: 0;
    align-items: center;
    margin-right: 8px;
    line-height: 46px;
    cursor: pointer;
    transition: background 0.3s;
    -webkit-tap-highlight-color: transparent;

    &:hover {
      background: var(--el-fill-color-lighter);
    }
  }

  .breadcrumb-container {
    flex-shrink: 0;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .topbar-container {
    display: flex;
    flex: 1;
    min-width: 0;
    align-items: center;
    margin-left: 8px;
    overflow: hidden;
  }

  .right-menu {
    display: flex;
    height: 100%;
    align-items: center;
    margin-left: auto;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-flex;
      height: 32px;
      align-items: center;
      justify-content: center;
      padding: 0 8px;
      border-radius: var(--app-radius-md);
      color: var(--el-text-color-regular);
      font-size: 18px;

      &.hover-effect {
        cursor: pointer;
        transition: background 0.2s ease, color 0.2s ease;

        &:hover {
          background: var(--el-fill-color-light);
          color: var(--el-color-primary);
        }
      }
    }

    .avatar-container {
      margin-right: 40px;

      .avatar-wrapper {
        position: relative;
        margin-top: 0;

        .user-avatar {
          display: block;
          width: 40px;
          height: 40px;
          margin-top: 0;
          border-radius: var(--app-radius-md);
          cursor: pointer;
        }

        i {
          position: absolute;
          top: 25px;
          right: -20px;
          font-size: 12px;
          cursor: pointer;
        }
      }
    }
  }
}
</style>
