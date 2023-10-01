FROM gcr.io/etcd-development/etcd:v3.4.27

COPY server.crt /etc/ssl/etcd/certificate.crt
COPY server.key /etc/ssl/etcd/private-key.key
COPY server.crt /etc/ssl/etcd/ca-certificate.crt

CMD ["/usr/local/bin/etcd", "--name", "infra0", "--data-dir", "infra0", "--client-cert-auth", \
    "--trusted-ca-file=/etc/ssl/etcd/ca-certificate.crt", "--cert-file=/etc/ssl/etcd/certificate.crt", \
    "--key-file=/etc/ssl/etcd/private-key.key", "--advertise-client-urls", "https://0.0.0.0:2379", \
    "--listen-client-urls", "https://0.0.0.0:2379"]