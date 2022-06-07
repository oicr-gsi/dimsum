module.exports = {
  content: [
    // files that Tailwind scans for
    'src/main/resources/templates/*.html',
    'ts/*.ts'
  ],
  theme: {
    fontFamily: {
      inter: ['Inter', 'sans-serif'],
      sarabun: ['Sarabun', 'sans-serif'],
    },
    fontSize: {
      8: '8px',
      10: '10px',
      12: '12px',
      16: '16px',
      32: '32px',
    },
    colors: {
      white: '#FFFFFF',
      black: '#28292F',
      yellow: '#F4F5AD',
      red: '#DF8484',
      grey: {
        100: '#F0F3F8',
        200: '#AAB0B8',
        300: '#7F858C',
      },
      green: {
        100: '#B3F0C0',
        200: '#5B9E5A',
        300: '#427441',
      }
    },
    borderWidth: {
      DEFAULT: '0.5px',
      '0': '0',
      '0.5': '0.5px',
      '1': '1px',
      '2': '2px',
    },

    extend: {},
  },
  plugins: [],
}
