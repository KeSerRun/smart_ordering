Component({
  properties: {
    value: { type: Number, value: 0 },
    max: { type: Number, value: 5 },
    size: { type: Number, value: 36 },
    readonly: { type: Boolean, value: false }
  },
  data: { stars: [] },
  observers: {
    'value,max'(value, max) {
      const stars = [];
      for (let i = 0; i < max; i++) {
        stars.push({ filled: i < value });
      }
      this.setData({ stars });
    }
  },
  methods: {
    onTap(e) {
      if (this.data.readonly) return;
      const idx = e.currentTarget.dataset.index;
      this.triggerEvent('change', { value: idx + 1 });
    }
  }
});
