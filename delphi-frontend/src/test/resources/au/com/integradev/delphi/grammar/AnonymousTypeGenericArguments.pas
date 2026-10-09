unit AnonymousTypeGenericArguments;

interface

type
  TFoo = class
    StaticArrays: TArray<array[Boolean] of Double>;
    DynamicArrays: TArray<array of Integer>;
    Sets: TArray<set of Byte>;
    Procedures: TArray<procedure>;
    Functions: TArray<function(Value: Integer): Integer>;
    Methods: TArray<procedure of object>;
    References: TArray<reference to procedure>;
    Mixed: TDictionary<string, array[0..2] of Double>;
  end;

implementation

end.