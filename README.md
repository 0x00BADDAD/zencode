# zencode
---

This project is a meta project. I intend to consolidate things like blogs, an IDE that can  
submit code to various platforms instead of locking to one and anything else in between.
---
## Build

I am using [gradle](https://docs.gradle.org/current/userguide/userguide.html) (kotlin DSL) as the build system for springboot backend.  
For the frontend I am using the well-known [webpack](https://webpack.js.org/) bundler.
---
## Building the project (for dev env only!)
### Backend
1. Move to the project root. Make sure gradle is already installed on your system.
2. Run the following 3 commands in that order:
```bash
   $ ./gradlew clean
   $ ./gradlew :weboot:bootJar
```
3. This will generate a .jar file for the backend and place it in the weboot/build/libs/ folder.  
Finally, run this to serve the backend locally on 8080:
```bash
   $ java -jar ./weboot/build/libs/weboot.jar
```
### Frontend
1. Move to the frontend dir located in the weboot/
```bash
    $ cd weboot/frontend/
```
2. Run:
```bash
    $ npm install
```
3. Then (first build the bundles and then) invoke the webpack dev server at port 3000 (with react refresh hot reloding enabled).  
dev server also proxies request to the backedn via the `/api/*` path prefix for any requests that  
the backend normally serves.
```bash
    $ npm run build
    $ npm run start
```
4. fetch `http:localhost:3000/api/hello` from you favourite client.
---
## LICENSE
Check the LICENSE file in the root of the project.
---
## Disclaimer
This Project is in primitive state. Expect breaking changes!
