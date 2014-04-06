# About

Phantasm is a real time communication tool. Powered by Play framework.

# Getting started

## Download the Phantasm

```git clone git@github.com:phantomtype/Phantasm.git```

## Setup securesocial

rename ```conf/securesocial.sample.conf``` to ```conf/securesocial.conf```

in ```conf/securesocial.conf```

write ```clientId``` and ```clientSecret``` of Facebook API.

## Setup front end

```
cd public
npm install
```

## Run Play application.

```play run```

and open the web browser ```http://localhost:9000/```

# Authorization with other than Facebook

Please see [SecureSocial](http://securesocial.ws/).
and edit ```conf/play.plugins```, ```conf/securesocial.conf```
