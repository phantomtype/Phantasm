# About

Phantasm is a real time communication tool. Powered by Play framework.

# Getting started

## Download the Phantasm.

```git clone https://github.com/phantomtype/Phantasm.git``` or donwload [zip archive](https://github.com/phantomtype/Phantasm/archive/master.zip).

## Setup secret.conf

copy ```conf/secret.sample.conf``` to ```conf/secret.conf```

in ```conf/secret.conf```

fill in ```application.secret=[secret key]```

## Setup securesocial.

copy ```conf/securesocial.sample.conf``` to ```conf/securesocial.conf```

in ```conf/securesocial.conf```

fill in ```clientId``` and ```clientSecret```.

## Setup database.

copy ```conf/database.sample.conf``` to ```conf/database.conf```


## Setup front end.

```
cd public
npm install
grunt
```

## Run as Play application.

```play run```

and open the web browser ```http://localhost:9000/```

# Authorization configuration.

Please see [SecureSocial](http://securesocial.ws/).
and edit ```conf/play.plugins```, ```conf/securesocial.conf```
