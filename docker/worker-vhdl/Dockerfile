











FROM debian:bookworm-slim

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
        ghdl \
        ca-certificates \
 && rm -rf /var/lib/apt/lists/*

RUN useradd --system --uid 10001 --home /work judge

WORKDIR /work
USER judge





ENTRYPOINT ["/bin/sh", "-c"]
CMD ["ghdl --version"]
