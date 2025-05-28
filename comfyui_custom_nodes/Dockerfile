FROM docker.mirror.hashicorp.services/library/python:3.10-slim

# Use Tsinghua University mirror for APT
RUN touch /etc/apt/sources.list && sed -i 's/deb.debian.org/mirrors.tuna.tsinghua.edu.cn/g' /etc/apt/sources.list
RUN sed -i 's/security.debian.org/mirrors.tuna.tsinghua.edu.cn/g' /etc/apt/sources.list

WORKDIR /opt/ComfyUI

RUN apt-get update --fix-missing -o Acquire::Retries=5 && \
    apt-get install -y --no-install-recommends git \
    && rm -rf /var/lib/apt/lists/* build-essential libgl1

RUN git clone https://github.com/comfyanonymous/ComfyUI . &&    pip install --retries 5 --timeout 60 --index-url https://pypi.tuna.tsinghua.edu.cn/simple -r requirements.txt

CMD ["python", "main.py", "--listen", "--port", "8188"]
