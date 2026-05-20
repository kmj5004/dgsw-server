











FROM debian:bookworm-slim

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
        iverilog \
        yosys \
        nodejs \
        npm \
        ca-certificates \
 && npm install -g --no-audit --no-fund netlistsvg \
 && apt-get purge -y --auto-remove npm \
 && rm -rf /var/lib/apt/lists/* /root/.npm /tmp/*


RUN useradd --system --uid 10001 --home /work judge

WORKDIR /work
USER judge












ENTRYPOINT ["/bin/sh", "-c"]
CMD ["iverilog -V"]
