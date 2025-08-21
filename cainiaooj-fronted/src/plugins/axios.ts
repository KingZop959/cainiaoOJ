// Add a request interceptor
import axios from "axios";
import { OpenAPI } from "../../generated";

axios.defaults.withCredentials = true;
// 允许通过环境变量覆盖后端基址；仅在未定义时回退（支持空字符串同源部署）
const apiBase = (process.env.VUE_APP_API_BASE ?? OpenAPI.BASE) as string;
axios.defaults.baseURL = apiBase;

axios.interceptors.request.use(
  function (config) {
    // Do something before request is sent
    return config;
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error);
  }
);

// Add a response interceptor
axios.interceptors.response.use(
  function (response) {
    // 不要改写返回值，保持与 generated 请求封装一致
    return response;
  },
  function (error) {
    return Promise.reject(error);
  }
);

export const request = axios;
export default axios;
