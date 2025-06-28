<template>
  <div class="admin-container">
    <h1>食尚阁 - 管理后台</h1>

    <!-- 上传菜品 -->
    <section class="card">
      <h2>上传菜品</h2>
      <input v-model="newDish.dishName" placeholder="菜品名称" />
      <input v-model.number="newDish.dishPrice" placeholder="价格" type="number" />
      <input v-model.number="newDish.dishStock" placeholder="库存" type="number" />
      <input type="file" @change="onImageSelected" accept="image/*" />
      <div v-if="previewImage" class="preview-img">
        <img :src="previewImage" alt="预览图" />
      </div>
      <button @click="uploadDish">上传</button>
    </section>

    <!-- 上传套餐 -->
    <section class="card">
      <h2>上传套餐</h2>
      <input v-model="newPackage.packageName" placeholder="套餐名称" />
      <input v-model.number="newPackage.discount" placeholder="折扣 (如0.8表示8折)" type="number" />

      <div class="dish-selection">
        <h3>选择菜品</h3>
        <div v-for="dish in dishes" :key="dish.id" class="dish-row">
          <span>{{ dish.dishName }}</span>
          <div class="actions">
            <button @click="addToPackage(dish)">+</button>
            <button @click="removeFromPackage(dish)">-</button>
          </div>
        </div>
      </div>

      <h4>已选菜品：</h4>
      <ul>
        <li v-for="dish in selectedDishes" :key="dish.id">{{ dish.dishName }}</li>
      </ul>

      <button @click="uploadPackage">上传套餐</button>
    </section>

    <!-- 删除菜品 -->
    <section class="card">
      <h2>删除菜品</h2>
      <ul class="dish-list">
        <li v-for="dish in dishes" :key="dish.id" class="dish-row">
          <span>{{ dish.dishName }}</span>
          <button @click="deleteDish(dish)">删除</button>
        </li>
      </ul>
    </section>

    <!-- 删除套餐 -->
    <section class="card">
      <h2>删除套餐</h2>
      <ul class="package-list">
        <li v-for="pkg in packages" :key="pkg.packageId" class="package-row">
          <span>{{ pkg.packageName }} - ￥{{ pkg.packagePrice }}</span>
          <button @click="deletePackage(pkg)">删除</button>
        </li>
      </ul>
    </section>

    <!-- 修改库存 -->
    <section class="card">
  <h2>修改库存</h2>
  <ul class="dish-list">
    <li v-for="dish in dishes" :key="dish.id" class="dish-row">
      <span>{{ dish.dishName }}</span>
      <input type="number" v-model.number="dish.newStock" placeholder="新库存" />
      <button @click="updateStock(dish)">修改</button>
    </li>
  </ul>
</section>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "AdminPage",
  data() {
    return {
      dishes: [],
      packages: [],
      newDish: {
        dishName: "",
        dishPrice: null,
        dishStock: 0,
      },
      newPackage: {
        packageName: "",
        discount: null,
      },
      selectedDishes: [],
      imageFile: null,
      previewImage: null,
    };
  },
  created() {
    this.loadDishes();
    this.loadPackages();
  },
  methods: {
    async loadDishes() {
      const res = await axios.get("http://localhost:8080/api/menu");
      this.dishes = res.data.map((dish) => ({ ...dish, newStock: null }));
    },
    async loadPackages() {
      const res = await axios.get("http://localhost:8080/api/packages");
      this.packages = res.data;
    },
    onImageSelected(e) {
      this.imageFile = e.target.files[0];
      if (this.imageFile) {
        this.previewImage = URL.createObjectURL(this.imageFile);
      }
    },
    async uploadDish() {
      if (!this.newDish.dishName || !this.newDish.dishPrice) {
        return alert("请填写完整菜品信息！");
      }
      const formData = new FormData();
      formData.append("dishName", this.newDish.dishName);
      formData.append("dishPrice", this.newDish.dishPrice);
      formData.append("dishStock", this.newDish.dishStock);
      if (this.imageFile) {
        formData.append("image", this.imageFile);
      }

      await axios.post("http://localhost:8080/api/menu/upload", formData);
      this.newDish.dishName = "";
      this.newDish.dishPrice = null;
      this.newDish.dishStock = 0;
      this.imageFile = null;
      this.previewImage = null;
      this.loadDishes();
      alert("菜品上传成功！");
    },
    addToPackage(dish) {
      if (!this.selectedDishes.includes(dish)) {
        this.selectedDishes.push(dish);
      }
    },
    removeFromPackage(dish) {
      this.selectedDishes = this.selectedDishes.filter((d) => d.id !== dish.id);
    },
    async uploadPackage() {
      if (!this.newPackage.packageName || !this.selectedDishes.length) {
        return alert("请填写套餐信息并选择菜品！");
      }

      const payload = {
        packageName: this.newPackage.packageName,
        discount: this.newPackage.discount,
        selectedItems: this.selectedDishes.map((d) => d.dishName),
      };

      await axios.post("http://localhost:8080/api/packages/upload", payload);
      this.newPackage.packageName = "";
      this.newPackage.discount = 1.0;
      this.selectedDishes = [];
      this.loadPackages();
      alert("套餐上传成功！");
    },
    async deleteDish(dish) {
      if (!confirm(`确定要删除菜品 ${dish.dishName} 吗？`)) return;
      await axios.delete(`http://localhost:8080/api/menu/${dish.id}`);
      this.loadDishes();
    },
    async deletePackage(pkg) {
      if (!confirm(`确定要删除套餐 ${pkg.packageName} 吗？`)) return;
      await axios.delete(`http://localhost:8080/api/packages/${pkg.packageId}`);
      this.loadPackages();
    },
    async updateStock(dish) {
      if (dish.newStock == null || dish.newStock < 0) {
        return alert("请输入合法的库存值！");
      }
      try {
        await axios.put(`http://localhost:8080/api/menu/${dish.id}/stock`, null, {
          params: { stock: dish.newStock },
        });
        alert("库存修改成功！");
        this.loadDishes();
      } catch (err) {
        console.error(err);
        alert("修改库存失败！");
      }
    },
  },
};
</script>

<style scoped>
.admin-container {
  width: 1060px;
  margin: auto;
  padding: 40px 20px;
  font-family: "Segoe UI", sans-serif;
  background: #f5f7fa;
}

h1 {
  text-align: center;
  margin-bottom: 40px;
  color: #2c3e50;
}

.card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

input[type="text"],
input[type="number"],
input[type="file"] {
  display: block;
  margin: 12px 0;
  padding: 10px;
  font-size: 16px;
  width: 100%;
  border-radius: 6px;
  border: 1px solid #ccc;
}

button {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  margin: 5px 5px 5px 0;
  cursor: pointer;
  transition: background 0.3s ease;
}

button:hover {
  background-color: #2980b9;
}

.preview-img {
  margin: 10px 0;
}

.preview-img img {
  max-width: 200px;
  border-radius: 8px;
}

.dish-selection {
  margin-top: 20px;
}

.dish-row {
  display: grid;
  grid-template-columns: 1fr 120px 100px;
  align-items: center;
  gap: 10px;
  margin: 8px 0;
  padding: 6px 0;
  border-bottom: 1px dashed #ddd;
}


.dish-row .actions {
  display: flex;
  gap: 8px;
}

.package-list {
  list-style: none;
  padding: 0;
}

.package-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 8px 0;
}
</style>
