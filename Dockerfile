FROM eclipse-temurin:8-jdk

ENV DEBIAN_FRONTEND=noninteractive

RUN sed -i 's@http://deb.debian.org/debian@http://mirrors.aliyun.com/debian@g' /etc/apt/sources.list \
    && sed -i 's@http://security.debian.org/debian-security@http://mirrors.aliyun.com/debian-security@g' /etc/apt/sources.list \
    && apt-get clean \
    && apt-get update --fix-missing \
    && apt-get install -y --fix-missing \
        time \
        gcc \
        g++ \
        python3 \
        pypy3 \
        golang \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /w

CMD ["/bin/sh", "-c", "while true; do sleep 3600; done"]