module.exports = {
	testEnvironment: 'jsdom',
	setupFilesAfterEnv: ['<rootDir>/test/setupTests.js'],
	transform: {
		'^.+\\.(js|jsx)$': 'babel-jest',
	},
	moduleNameMapper: {
		'\\.(css|less|scss|sass)$': 'identity-obj-proxy',
		'\\.(svg|png|jpg|jpeg|gif|webp)$': '<rootDir>/test/__mocks__/fileMock.js',
		'^/.+\\.(svg|png|jpg|jpeg|gif|webp)$': '<rootDir>/test/__mocks__/fileMock.js',
	},
};
