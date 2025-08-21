FROM debian:unstable-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jre-headless \
    python3 \
    libstdc++6 libgcc-s1 \
    ca-certificates \
 && rm -rf /var/lib/apt/lists/*

# 可选：验证符号版本是否满足
RUN ldd --version || true
RUN bash -lc 'strings $(ldconfig -p | awk "/libstdc\+\+\.so\.6/ {print \$NF; exit}") | grep -o "GLIBCXX_[0-9.]*" | sort -V | tail -1'

WORKDIR /app

# 健康检查：检查 /app 是否存在；python/java 能否调用（都成功才算健康）
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD bash -lc 'test -d /app && python3 -V >/dev/null 2>&1 && java -version >/dev/null 2>&1 || exit 1'
