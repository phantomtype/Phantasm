# About

Phantasm is a real time communication tool. Powered by Play framework.

# Getting started

## Download the Phantasm

```git clone git@github.com:phantomtype/Phantasm.git``` or donwload [zip archive](https://github.com/phantomtype/Phantasm/archive/master.zip).

## Setup securesocial

rename ```conf/securesocial.sample.conf``` to ```conf/securesocial.conf```

in ```conf/securesocial.conf```

edit ```clientId``` and ```clientSecret``` to corresponding Facebook API.

## Setup database

rename ```conf/database.sample.conf``` to ```conf/database.conf```


## Setup front end

```
cd public
npm install
grunt
```

## Run as Play application.

```play run```

and open the web browser ```http://localhost:9000/```

# Authorization other than Facebook

Please see [SecureSocial](http://securesocial.ws/).
and edit ```conf/play.plugins```, ```conf/securesocial.conf```
