<template>
  <div class="upload-container">
    <a-card class="upload-card">
      <h2>发布商品</h2>
      <a-form 
        ref="formRef"
        :model="form" 
        layout="vertical" 
        @finish="handleSubmit"
      >
        <!-- 商品名称 -->
        <a-form-item
          label="商品名称"
          name="name"
          :rules="[{ required: true, message: '请输入商品名称' }]"
        >
          <a-input v-model:value="form.name" placeholder="请输入商品名称" />
        </a-form-item>

        <!-- 商品描述 -->
        <a-form-item
          label="商品描述"
          name="description"
          :rules="[{ required: true, message: '请输入商品描述' }]"
        >
          <a-textarea v-model:value="form.description" :rows="4" placeholder="请输入商品描述" />
        </a-form-item>

        <!-- 商品类别 -->
        <a-form-item
          label="商品类别"
          name="category"
          :rules="[{ required: true, message: '请选择商品类别' }]"
        >
          <a-select v-model:value="form.category" placeholder="请选择商品类别">
            <a-select-option value="电子产品">电子产品</a-select-option>
            <a-select-option value="图书教材">图书教材</a-select-option>
            <a-select-option value="生活用品">生活用品</a-select-option>
            <a-select-option value="服装服饰">服装服饰</a-select-option>
            <a-select-option value="其他">其他</a-select-option>
          </a-select>
        </a-form-item>

        <!-- 价格 -->
        <a-form-item
          label="价格"
          name="price"
          :rules="[{ required: true, message: '请输入价格' }]"
        >
          <a-input-number
            v-model:value="form.price"
            :min="0"
            :precision="2"
            style="width: 100%"
            placeholder="请输入价格"
          />
        </a-form-item>

        <!-- 校区 -->
        <a-form-item
          label="校区"
          name="campus"
          :rules="[{ required: true, message: '请选择校区' }]"
        >
          <a-select v-model:value="form.campus" placeholder="请选择校区">
            <a-select-option value="南望山-西区">南望山-西区</a-select-option>
            <a-select-option value="南望山-东区">南望山-东区</a-select-option>
            <a-select-option value="南望山-北区">南望山-北区</a-select-option>
            <a-select-option value="未来城校区">未来城校区</a-select-option>
          </a-select>
        </a-form-item>

        <!-- 图片上传 -->
        <a-form-item
          label="商品图片"
          name="files"
          :rules="[{ required: true, validator: validateFiles }]"
        >
          <a-upload
            v-model:file-list="fileList"
            list-type="picture-card"
            :max-count="5"
            :before-upload="beforeUpload"
            @preview="handlePreview"
            @remove="handleRemove"
            @change="handleChange"
            :customRequest="() => false"
            :action="null"
          >
            <div v-if="fileList.length < 5">
              <plus-outlined />
              <div class="ant-upload-text">上传</div>
            </div>
          </a-upload>
          <a-modal :visible="previewVisible" :footer="null" @cancel="handleCancel">
            <img alt="example" style="width: 100%" :src="previewImage" />
          </a-modal>
        </a-form-item>

        <!-- 提交按钮 -->
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading" block>
            发布商品
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useStore } from 'vuex'
import axios from 'axios'
import { useRouter } from 'vue-router'

const store = useStore()
const router = useRouter()
const loading = ref(false)
const fileList = ref([])
const previewVisible = ref(false)
const previewImage = ref('')
const formRef = ref(null)

const form = reactive({
  name: '',
  description: '',
  category: undefined,
  price: undefined,
  campus: undefined,
  files: []
})

// 图片上传前的验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB！')
    return false
  }
  return false  // 返回false阻止自动上传
}

// 处理图片预览
const handlePreview = async (file) => {
  if (!file.url && !file.preview) {
    file.preview = await getBase64(file.originFileObj)
  }
  previewImage.value = file.url || file.preview
  previewVisible.value = true
}

// 将文件转换为Base64
const getBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = error => reject(error)
  })
}

// 处理图片取消预览
const handleCancel = () => {
  previewVisible.value = false
}

// 处理图片移除
const handleRemove = (file) => {
  const index = fileList.value.indexOf(file)
  const newFileList = fileList.value.slice()
  newFileList.splice(index, 1)
  fileList.value = newFileList
}

// 自定义文件验证函数
const validateFiles = async (rule, value) => {
  if (fileList.value.length === 0) {
    throw new Error('请上传商品图片')
  }
}

// 处理图片上传状态改变
const handleChange = ({ fileList: newFileList }) => {
  fileList.value = newFileList.map(file => {
    if (file.status === 'error') {
      file.status = 'done'
    }
    return file
  })
  // 更新表单的 files 字段
  form.files = fileList.value.map(file => file.originFileObj)
}

// 处理表单提交
const handleSubmit = async (values) => {
  try {
    // 检查用户是否登录
    if (!store.state.isLoggedIn || !store.state.userInfo) {
      message.warning('请先登录')
      router.push('/login')
      return
    }

    // 验证表单
    await formRef.value.validate()

    loading.value = true
    
    // 创建 FormData 对象
    const formData = new FormData()
    formData.append('name', form.name)
    formData.append('description', form.description)
    formData.append('category', form.category)
    formData.append('price', form.price)
    formData.append('campus', form.campus)
    formData.append('userId', store.state.userInfo.id)
    
    // 添加图片文件
    fileList.value.forEach(file => {
      formData.append('files', file.originFileObj)
    })
    
    // 发送请求
    const response = await axios.post('http://localhost:8080/api/items/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    // 检查响应数据
    console.log('商品发布响应:', response.data)
    
    if (response.data && response.data.id) {
      message.success('商品发布成功！')
      // 清空表单
      form.name = ''
      form.description = ''
      form.category = undefined
      form.price = undefined
      form.campus = undefined
      fileList.value = []
      
      // 等待一秒后跳转，给后端一些处理时间
      setTimeout(() => {
        router.push({
          name: 'ProductDetail',
          params: { id: response.data.id }
        })
      }, 1000)
    } else {
      message.success('商品发布成功！')
      // 跳转到商品列表页
      router.push('/')
    }
  } catch (error) {
    if (error.errorFields) {
      // 表单验证错误
      message.error('请填写完整的表单信息')
    } else {
      console.error('商品发布失败:', error)
      message.error(error.response?.data || '商品发布失败，请重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.upload-container {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.upload-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.upload-card h2 {
  margin-bottom: 24px;
  text-align: center;
  color: #333;
}

:deep(.ant-upload-list-picture-card-container) {
  width: 128px;
  height: 128px;
}

:deep(.ant-upload.ant-upload-select-picture-card) {
  width: 128px;
  height: 128px;
}
</style> 
