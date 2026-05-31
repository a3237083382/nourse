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
  background: #f4f5f2;
  color: #1f252b;
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
}

button::after {
  border: 0;
}

input,
textarea {
  min-width: 0;
}
</style>
