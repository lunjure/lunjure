# lunjure

_lunjure_ is a web-based lunch planner for groups. It allows users to discuss the where and when of their lunch break in a web chat. The real benefit is that in addition to free-form chat, it provides some domain-specific commands that make it a lot easier for all participants to see who would like to go to which venue for lunch, and at which time. Moreover, the geo-location of the participants are used to provide data about venues from Foursquare. 

This initial version was made with love in Cologne, Germany, at the [Advance Hackathon 2012](http://hackathon.advance-conference.com/), within the time frame of one and a half days.

From a technical perspective, _lunjure_ was a way for us to prove to ourselves and others how well Clojure and ClojureScript are suited for writing a state-of-the-art one-page HTML5 and CSS3 web application. It might also serve as a nice demo app of how to make use of web sockets using ClojureScript on the client and the Netty-based Aleph on the server side.

However, _lunjure_ is more than a tech-demo. The idea for the project was born out of a pain experienced by us on a daily basis, and we are planning to continue the development on this project.

## Status quo

_lunjure_ is still in an early stage. At the moment, it only provides a single conference room/group. If you are like us and see a value in using it for planning your lunches, it is perfectly possible to run it on your own server, for instance in your company's intranet.

## Future

The following are some of the missing features:

- Allowing users to create their own conference rooms/groups and invite others to it
- Automatic check-in at Foursquare at the time you specified for your lunch
- All kinds of recommendations
- Multiple themes (for those who do not like our retro terminal style)
- Using NLP so we do not have to rely on structured commands

## Usage

1. Install Redis (`brew install redis` if you are on OS X).
2. Clone the repository, and `cd` into it.
3. Register a Foursquare consumer.
4. Copy the file `src/foursquare_config_sample.clj` to `src/foursquare_config.clj` and enter your Foursquare client id, client secret and the redirect URI you chose for your app.
5. Run `lein run`.
6. Lunjure can now be accessed at port 8080.

As a user, you are currently required to use Foursquare for authentication.

Here is an example of using the commands available in _lunjure_, which use Lisp syntax:

- `(team fastfood 1230 "McDonalds")` - For the last argument, the location, we provide auto-completion based on the location set for the group and the Foursquare venues in that area.
- `(join fastfood)` - you join the previously created team with the specified name.
- `(leave)` - you leave your current team and are now back to _undecided_.
- `(geolocation)` - send your location to the server and have it set as the new geo-location of the conference room.

## License

Copyright (C) 2012 Moritz Heidkamp, Daniel Westheide, Moritz Ulrich, Gregor Adams, Markus Strickler, Oliver Twardowski

Distributed under the Eclipse Public License, the same as Clojure.
