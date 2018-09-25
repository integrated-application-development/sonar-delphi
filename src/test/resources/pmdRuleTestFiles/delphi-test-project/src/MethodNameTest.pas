unit MethodNameTest;

interface

type

  IRecyclable = Interface(IInterface)

    // Non-compliant: function name should begin with a capital letter
    function getIsRecyclable : Boolean;

    property isRecyclable : Boolean read getIsRecyclable;

  end;

implementation

end.
