# 🚀 Meu Laboratório de Kubernetes

Bem-vindo(a) ao meu repositório público de estudos!

Criei este guia para documentar a configuração do meu ambiente de laboratório local e ajudar outras pessoas que também estão dando os primeiros passos no mundo de **Containers e Kubernetes**.

Aqui, mostro exatamente os comandos que utilizei para instalar o **Docker**, o **Kubectl** e o **Kind** em um ambiente Linux, detalhando o que cada linha faz para que não executemos scripts "às cegas".

---

## 🛠️ 1. Instalando o Docker

O Docker é o motor de contêineres que servirá como base para rodar as "máquinas" (nós) do nosso cluster Kubernetes local.

```bash
curl -fsSL https://get.docker.com | bash 

```

* **O que faz:** O comando `curl` baixa um script oficial da internet (de forma silenciosa por causa do `-fsSL`) e o passa diretamente para o interpretador de comandos (`| bash`) para ser executado.
* **Por que fizemos:** É o método oficial e mais rápido para instalar a versão mais recente do Docker (Community Edition) no Linux, configurando todos os repositórios necessários automaticamente.

---

## 🎮 2. Instalando o Kubectl

O `kubectl` é a nossa "ferramenta de controle". É a linha de comando oficial que usamos para gerenciar o cluster (criar pods, serviços, etc.).

```bash
# Baixa o binário mais recente
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"

# Instala o binário no path do sistema
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl 

```

* **O que faz:** O primeiro comando baixa o executável. O trecho `$(...)` consulta a versão estável mais recente automaticamente. O segundo comando move o arquivo para `/usr/local/bin/`, define o `root` como dono e concede permissões de execução (`0755`).
* **Por que fizemos:** Isso "instala" o comando globalmente. Assim, você pode digitar `kubectl` em qualquer diretório do terminal.

### ⚡ 2.1 Configurando o Autocomplete (Produtividade)

Para evitar digitar comandos extensos, configuramos o "TAB" para autocompletar e criamos o atalho `k`.

```bash
# Instala o suporte ao autocomplete
sudo apt-get install -y bash-completion

# Gera e salva as regras de autocomplete do kubectl
kubectl completion bash | sudo tee /etc/bash_completion.d/kubectl > /dev/null
sudo chmod a+r /etc/bash_completion.d/kubectl

# Cria o alias 'k' e configura o autocomplete para ele
echo 'alias k=kubectl' >> ~/.bashrc
echo 'complete -o default -F __start_kubectl k' >> ~/.bashrc 

# Aplica as mudanças no terminal atual
source ~/.bashrc

```

---

## 📦 3. Instalando o Kind (Kubernetes in Docker)

O **Kind** permite subir clusters Kubernetes inteiros rodando dentro de contêineres Docker. É a ferramenta perfeita para estudos locais.

```bash
# Baixa o binário para arquitetura x86_64
[ $(uname -m) = x86_64 ] && curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.31.0/kind-linux-amd64

# Torna o arquivo executável
chmod +x ./kind

# Move para o path global
sudo mv ./kind /usr/local/bin/kind

```

* **O que faz:** Verifica se o processador é 64 bits, baixa a versão `v0.31.0` e concede permissão de execução (`+x`).
* **Por que fizemos:** Por segurança, o Linux não permite executar arquivos baixados sem permissão explícita. Ao mover para `/usr/local/bin`, habilitamos o uso de comandos como `kind create cluster` em qualquer lugar.
