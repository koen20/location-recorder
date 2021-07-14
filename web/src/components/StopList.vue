<template>
  <table class="table">
    <thead>
      <th>Naam</th>
      <th>Stad</th>
      <th>Land</th>
      <th>Datum opgeslagen</th>
    </thead>
    <tbody>
    <tr v-for="stop in stops" :key="stop.stopId">
      <td>{{stop.name}}</td>
      <td>{{stop.city}}</td>
      <td>{{stop.country}}</td>
      <td>{{stop.dateFetched}}</td>
    </tr>
    </tbody>
  </table>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      stops: [],
      errors: []
    }
  },
  name: "StopList",

  async created() {
    try {
      const response = await axios.get(`http://127.0.0.1:9936/api/stop`)
      this.stops = response.data
    } catch (e) {
      this.errors.push(e)
    }
  }
}
</script>

<style scoped>

</style>