<template>
  <v-container
    id="input-usage"
    fluid
  >
    <v-row>
        <v-col cols="3">
             <v-icon size="100">mdi-weather-partly-cloudy</v-icon>
        </v-col>
        <v-col cols="6">
            <v-form ref="form" class="pb-4">
                <v-text-field label="Gateway URL" v-model="apiurl"/>
                <v-btn class="mr-4" @click="getWeather">
                    Go
                </v-btn>
            </v-form>
        </v-col>
        
    </v-row>
    <hr/>
    <v-row>
      <v-col cols="1">
      </v-col>
      <v-col cols="10">
      <v-table>
       
        <template v-slot:default>
          <thead>
            <tr>
              <th class="text-left">City</th>
              <th class="text-left">Weather</th>
              <th class="text-left"></th>
            </tr>
          </thead>
          <transition name="fade">
          <tbody v-if="show">
            <tr v-for="city in cities" :key="city.name">
              <td>{{ city.name }}</td>
              <td>{{ city.description }}</td>
              <td><v-icon size="40">mdi-{{ city.icon }}</v-icon></td>
            </tr>
          </tbody>
          </transition>
        </template>
      </v-table>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
  export default {
    data() {
        return {
            apiurl: "https://GATEWAY_NAME.CONTAINER_APP_ENV_DNS_SUFFIX",
            cities: {},
            show: true
        }
    },
    props: {
    },
    methods: {
      async getWeather() {
          this.show = false;
          const citiesResponse = await fetch(this.apiurl + '/city-service/cities');
          const citiesData = await citiesResponse.json();

          const weatherPromises = citiesData.map(city => {
            const url = this.apiurl + '/weather-service/weather/city?name=' + encodeURI(city.name);
            return fetch(url).then(response => response.json());
          });

          const weatherData = await Promise.all(weatherPromises);
          this.cities = weatherData.map((response) => {
            const city = {};
            city.name = response.city;
            city.description = response.description;
            city.icon = response.icon;
            return city;
          });
          this.show = true;
      }
    },
  }
</script>

<style>
.fade-enter-active, .fade-leave-active {
  transition: opacity .5s;
}
.fade-enter, .fade-leave-to /* .fade-leave-active below version 2.1.8 */ {
  opacity: 0;
}
</style>