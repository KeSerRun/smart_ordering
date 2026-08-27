Component({
  properties: {
    dish: { type: Object, value: {} },
    showAddBtn: { type: Boolean, value: true }
  },
  methods: {
    onTap() {
      this.triggerEvent('tap', { dish: this.data.dish });
    },
    onAdd() {
      if (this.data.dish.status === 0) return;
      this.triggerEvent('add', { dish: this.data.dish });
    }
  }
});
