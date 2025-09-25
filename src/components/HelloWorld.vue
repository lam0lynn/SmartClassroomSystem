<template>  
  <div>{{ msg }}</div>  
</template>  

<script>  
import { getHello } from '@/api/user' // 对应你刚封装的api文件  

export default {  
  data() {  
    return { msg: '' }  
  },  
  async mounted() {  
    const res = await getHello()  
    this.msg = res.data  
  }  
}  
</script>  