# Rage4J Docusaurus

This website is built using [Docusaurus](https://docusaurus.io/), a modern static website generator.

### Installation

```
$ npm install
```

### Local Development

```
$ npm start
```

This command starts a local development server and opens up a browser window. Most changes are reflected live without
having to restart the server.

### Build

```
$ npm run build
```

This command generates static content into the `build` directory and can be served using any static contents hosting
service.

### Docker

The project includes a multi-stage Dockerfile for containerized development and deployment.

#### Development

Build and run the development container:

```
$ docker build --target development -t rage4j-docs:dev .
$ docker run -p 3000:3000 rage4j-docs:dev
```

The development server will be available at `http://localhost:3000`.

#### Production Build

Build the static site:

```
$ docker build --target production -t rage4j-docs:prod .
```

#### Deploy with Nginx

Build and run the production container with Nginx:

```
$ docker build --target deploy -t rage4j-docs:deploy .
$ docker run -p 80:80 rage4j-docs:deploy
```

The site will be served at `http://localhost`.