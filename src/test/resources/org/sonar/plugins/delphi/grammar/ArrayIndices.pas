unit ArrayIndices;

interface

const
  C_StringArrayWithTypeID: array[Integer] of String = (
    'String1',
    'String2',
    'String3'
  );

  C_StringArrayWithNumericSubRange: array[0..2] of String = (
    'String1',
    'String2',
    'String3'
  );

  C_StringArrayWithExpressionSubRange: array[LowerBound..UpperBound] of String = (
    'String1',
    'String2',
    'String3'
  );

implementation

end.
