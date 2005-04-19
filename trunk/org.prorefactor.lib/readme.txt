Most of these libraries can be commonly shared amongst plug-ins, but there is a problem
with trying to reference hibernate.jar from one plug-in to another. Each plug-in gets its own
ClassLoader, and Hibernate needs to be able to find resources on your classpath. So if
hibernate.jar has a different ClassLoader than your plug-in does, then the necessary
resources won't be found by Hibernate. Because of this, you must copy hibernate.jar into
your plug-in, so that it has the same class loader as the rest of your plug-in.
If you know of a better way, please tell me.  -- john@joanju.com, March 2005.
