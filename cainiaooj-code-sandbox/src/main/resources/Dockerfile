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