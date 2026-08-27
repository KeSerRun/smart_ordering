Component({
  properties: {
    count: { type: Number, value: 0 },
    total: { type: Number, value: 0 },
    submitText: { type: String, value: '去结算' },
    visible: { type: Boolean, value: true }
  },
  methods: {
    onCartTap() { this.triggerEvent('carttap'); },
    onSubmit() {
      if (this.data.count === 0) return;
      this.triggerEvent('submit');
    }
  }
});
