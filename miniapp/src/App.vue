<script>
export default {
  onLaunch: function () {
    console.log('App Launch')
  },
  onShow: function () {
    console.log('App Show')
  },
  onHide: function () {
    console.log('App Hide')
  },
}
</script>

<style>
page {
  min-height: 100%;
  background: #fff7f2;
  color: #222832;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif;
}

view,
text,
input,
textarea,
button {
  box-sizing: border-box;
}

button {
  margin: 0;
  padding: 0;
  border: 0;
  font-weight: 600;
  transition: opacity 0.18s ease;
}

button::after {
  border: 0;
}

button[disabled],
button:active {
  opacity: 0.78;
}

input,
textarea {
  min-width: 0;
}

.page {
  background: #fff7f2;
}
</style>
