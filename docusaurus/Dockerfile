## Base ##
FROM node:lts AS base

ENV NPM_CONFIG_LOGLEVEL=warn
ENV NPM_CONFIG_COLOR=false

WORKDIR /home/node/app
COPY --chown=node:node . /home/node/app/

## Development ##
FROM base AS development
WORKDIR /home/node/app
RUN npm install
USER node
EXPOSE 3000
CMD ["npm", "start"]

## Production ##
FROM base AS production
WORKDIR /home/node/app
COPY --chown=node:node --from=development /home/node/app/node_modules /home/node/app/node_modules
RUN npm run build

## Deploy ##
FROM nginx:stable-alpine AS deploy
WORKDIR /home/node/app
COPY --chown=node:node --from=production /home/node/app/build /usr/share/nginx/html/
