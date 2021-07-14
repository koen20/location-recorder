<template>
  <div id="nav">
    <router-link to="/">Home</router-link> |
    <router-link to="/stops">Stops</router-link>
  </div>
  <router-view/>
</template>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

#nav {
  padding: 30px;
}

#nav a {
  font-weight: bold;
  color: #2c3e50;
}

#nav a.router-link-exact-active {
  color: #42b983;
}

#app {
  font-family: -apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,Oxygen-Sans,Ubuntu,Cantarell,"Helvetica Neue",sans-serif;
  height: 98vh;
  width: 98vw;
  margin: 0;
  display: grid;
  grid-template-rows: auto 1fr;
}

.route-icon {
  height: 22px;
  margin-right: 5px;
}

.stop {
  background-color: #d7d7d7;
}

.button-add-stop{
  margin-left: 2px;
}
</style>
