FROM tfb/hhvm-php5:latest

ADD ./ /slim
WORKDIR /slim

RUN composer.phar install --no-progress

CMD hhvm -m daemon --config /slim/deploy/config.hdf && \
    nginx -c /slim/deploy/nginx-hhvm.conf -g "daemon off;"